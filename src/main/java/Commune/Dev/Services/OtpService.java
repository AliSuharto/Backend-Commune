package Commune.Dev.Services;

import Commune.Dev.Models.OtpTemp;
import Commune.Dev.Repositories.OtpTempRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class OtpService {
    private final OtpTempRepository otpRepo;
    @Value("${app.otp.length:6}")
    private int length;
    @Value("${app.otp.ttl-minutes:10}")
    private int ttlMinutes;

    public OtpService(OtpTempRepository otpRepo) {
        this.otpRepo = otpRepo;
    }

    public OtpTemp createOtpForEmail(String email) {
        String code = generateNumericCode(length);
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(ttlMinutes));
        OtpTemp otp = new OtpTemp();
        otp.setEmail(email);
        otp.setCode(code);
        otp.setExpiresAt(expiresAt);
        otpRepo.save(otp);
        return otp;
    }

    public boolean verifyCode(String email, String code) {
        Optional<OtpTemp> otpOpt = otpRepo.findTopByEmailOrderByCreatedAtDesc(email);
        if (otpOpt.isEmpty()) return false;
        OtpTemp otp = otpOpt.get();
        if (otp.getExpiresAt().isBefore(Instant.now())) return false;
        return otp.getCode().equals(code);
    }

    public void deleteOtp(String email) {
        otpRepo.deleteByEmail(email);
    }

    private String generateNumericCode(int length) {
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) sb.append(r.nextInt(10));
        return sb.toString();
    }
}

