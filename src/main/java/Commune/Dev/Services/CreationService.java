package Commune.Dev.Services;

import Commune.Dev.Dtos.UserDto;
import Commune.Dev.Models.*;
import Commune.Dev.Repositories.*;
import Commune.Dev.Request.FinalizeRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CreationService {
    private final OtpService otpService;
    private final UserRepository ordRepo;
    private final CommuneRepository communeRepo;
    private final OtpTempRepository otpTempRepo;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final UserActivityRepository userActivityRepository;

    @Autowired  // optional in newer Spring versions
    public CreationService(OtpService otpService,
                           UserRepository ordRepo,
                           CommuneRepository communeRepo,
                           OtpTempRepository otpTempRepo,
                           MailService mailService,
                           PasswordEncoder passwordEncoder,
                           UserActivityRepository userActivityRepository
                           ) {
        this.otpService = otpService;
        this.ordRepo = ordRepo;
        this.communeRepo = communeRepo;
        this.otpTempRepo = otpTempRepo;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.userActivityRepository=userActivityRepository;
    }






    // Step 1: init -> generate OTP and send mail
    public void initOrdonnateur(UserDto dto) {
        // optionally: check if email already used
        if (ordRepo.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }
        OtpTemp otp = otpService.createOtpForEmail(dto.email());
        mailService.sendOtpEmail(dto.email(), otp.getCode());
    }

    // Step 2: finalize -> transactional creation
    @Transactional
    public void finalizeCreation(FinalizeRequest req) {
        boolean ok = otpService.verifyCode(req.email(), req.validationCode());
        if (!ok) {
            throw new IllegalArgumentException("Code invalide ou expiré");
        }

        // double-check uniqueness
        if (ordRepo.existsByEmail(req.ordonnateur().email())) {
            throw new IllegalStateException("Email déjà présent");
        }

        // Create ordonnateur (hash password)
        User o = new User();
        o.setNom(req.ordonnateur().nom());
        o.setRole(Roletype.ORDONNATEUR);
        o.setPrenom(req.ordonnateur().prenom());
        o.setEmail(req.ordonnateur().email());
        o.setPassword(passwordEncoder.encode(req.ordonnateur().password()));
        o.setTelephone(req.ordonnateur().telephone());
        ordRepo.save(o);

        // Create commune
        Commune c = new Commune();
        c.setNom(req.commune().nom());
        c.setPays(req.commune().pays());
        c.setCodePostal(req.commune().nom());
        c.setLocalisation(req.commune().localisation());
        c.setRegion(req.commune().region());
        c.setTelephone(req.commune().telephone());
        c.setMail(req.commune().mail());
        communeRepo.save(c);


        UserActivity activity = new UserActivity();
        activity.setUser(o);
        activity.setLoginCount(0);
        activity.setHasUsedApp(false);
        activity.setFirstLogin(null);
        activity.setLastLogin(null);
        userActivityRepository.save(activity);

        // remove OTP to avoid reuse
        otpService.deleteOtp(req.email());
    }
}

