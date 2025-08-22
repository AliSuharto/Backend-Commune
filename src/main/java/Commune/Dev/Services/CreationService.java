package Commune.Dev.Services;

import Commune.Dev.Dtos.OrdonnateurDto;
import Commune.Dev.Models.Commune;
import Commune.Dev.Models.Ordonnateur;
import Commune.Dev.Models.OtpTemp;
import Commune.Dev.Repositories.CommuneRepository;
import Commune.Dev.Repositories.OrdonnateurRepository;
import Commune.Dev.Repositories.OtpTempRepository;
import Commune.Dev.Request.FinalizeRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CreationService {
    private final OtpService otpService;
    private final OrdonnateurRepository ordRepo;
    private final CommuneRepository communeRepo;
    private final OtpTempRepository otpTempRepo;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired  // optional in newer Spring versions
    public CreationService(OtpService otpService,
                           OrdonnateurRepository ordRepo,
                           CommuneRepository communeRepo,
                           OtpTempRepository otpTempRepo,
                           MailService mailService,
                           PasswordEncoder passwordEncoder) {
        this.otpService = otpService;
        this.ordRepo = ordRepo;
        this.communeRepo = communeRepo;
        this.otpTempRepo = otpTempRepo;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }






    // Step 1: init -> generate OTP and send mail
    public void initOrdonnateur(OrdonnateurDto dto) {
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
        boolean ok = otpService.verifyCode(req.email(), req.code());
        if (!ok) {
            throw new IllegalArgumentException("Code invalide ou expiré");
        }

        // double-check uniqueness
        if (ordRepo.existsByEmail(req.ordonnateur().email())) {
            throw new IllegalStateException("Email déjà présent");
        }

        // Create ordonnateur (hash password)
        Ordonnateur o = new Ordonnateur();
        o.setNom(req.ordonnateur().nom());
        o.setPrenom(req.ordonnateur().prenom());
        o.setEmail(req.ordonnateur().email());
        o.setPassword(passwordEncoder.encode(req.ordonnateur().password()));
        ordRepo.save(o);

        // Create commune
        Commune c = new Commune();
        c.setNom(req.commune().nom());
        c.setAdresse(req.commune().adresse());
        c.setRegion(req.commune().region());
        communeRepo.save(c);

        // remove OTP to avoid reuse
        otpService.deleteOtp(req.email());
    }
}

