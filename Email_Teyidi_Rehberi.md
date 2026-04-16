# Salon Manager - Email Təsdiqi (Verification) Sistemi Qurulum Rəhbəri

Bu sənəd sistemdə qeydiyyatdan keçən istifadəçilərə təsdiq kodu göndərmək və hesablarını aktivləşdirmək üçün lazım olan bütün sətir-sətir əməliyyatları ehtiva edir.

---

## Addım 1: Kitabxananın Əlavə Edilməsi (`build.gradle`)
`dependencies` blokunun daxilinə bu kodu əlavə edin və Gradle-i yeniləyin (Load/Sync edib).
```gradle
implementation 'org.springframework.boot:spring-boot-starter-mail'
```

---

## Addım 2: Konfiqurasiya (`application.yaml`)
`spring:` bölməsinin daxilinə (məsələn, `jpa:` və `datasource:` olan yerin aşağısına) bunu əlavə edin.
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: seninEmailin@gmail.com   # Bura öz gmail hesabını yaz
    password: google_app_password      # Google-dan alacağın 16 hərflik xüsusi açar
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```
> **Diqqət!** Adi Gmail şifrəniz bura yazılmır. Google hesabınıza girib hesab parametrlərindən `Təhlükəsizlik -> 2-addımlı təsdiq -> App Passwords` ardıcıllığı ilə yaradılan şifrəni götürməlisiniz.

---

## Addım 3: Databazanı Hazırlamaq (`UserEntity.java`)
`UserEntity`-yə bu yeni field-ləri (sütunları) əlavə edin:
```java
    @Column(name = "verification_code", length = 6)
    private String verificationCode;

    @Column(name = "verification_expires_at")
    private java.time.Instant verificationExpiresAt;
```

---

## Addım 4: Məktubu Göndərən Mexanizm (`EmailService.java`)
`service` qovluğunda yeni bir fayl yaradın və aşağıdakı kodu yazın:
```java
package com.nurlansuleymanli.salonmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Salon Manager - Email Təsdiqi");
        message.setText("Salam! Sizin qeydiyyat təsdiq kodunuz: " + code);
        
        mailSender.send(message);
    }
}
```

---

## Addım 5: İş Məntiqi (`AuthService.java`)
`AuthService`-in içindəki **`registerUser`** metodunda istifadəçini `save()` etməzdən əvvəl bu sətrləri əlavə edin:
```java
// 1. 6 Rəqəmli random kod yaradırıq
String randomCode = String.format("%06d", new java.util.Random().nextInt(999999));

// 2. Kodu istifadəçiyə calaşdır və hesabını 'false' (Təsdiqlənməmiş) edirik
user.setVerificationCode(randomCode);
user.setVerificationExpiresAt(java.time.Instant.now().plus(15, java.time.temporal.ChronoUnit.MINUTES));
user.setActive(false); 

userRepository.save(user); // İstifadəçi yaradılır

// 3. Email-i göndəririk 
// (Bunun üçün AuthService-in yuxarısında 'private final EmailService emailService;' əlavə etməyi unutmayın)
emailService.sendEmail(user.getEmail(), randomCode);
```

Sonra isə `AuthService` içində **kodu yoxlayacaq** bu yeni metodu tamamən əlavə edin:
```java
public org.springframework.http.ResponseEntity<?> verifyCode(String email, String enteredCode) {
    com.nurlansuleymanli.salonmanager.model.entity.UserEntity user = 
            userRepository.findByEmail(email).orElseThrow();
    
    // Yoxlayırıq kod düzdürmü və vaxtı bitməyib ki?
    if(user.getVerificationCode() != null && 
       user.getVerificationCode().equals(enteredCode) && 
       user.getVerificationExpiresAt().isAfter(java.time.Instant.now())) {
        
        user.setActive(true); // Hesabı canlandır (Login olmağa icazə vet)
        user.setVerificationCode(null); // Kodu sıfırla ki bir də istifadə edilməsin
        userRepository.save(user);
        
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("message", "Hesab uğurla aktiv edildi. İndi daxil ola bilərsiniz."));
    }
    return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "Kod səhvdir və ya vaxtı bitib."));
}
```

---

## Addım 6: Təsdiq Ucu (`AuthController.java`)
Sonda `AuthController` faylına girin və bu endpointi daxil edin ki, Frontend sizinlə əlaqə qura bilsin:
```java
@PostMapping("/verify-email")
public ResponseEntity<?> verifyEmail(@RequestParam String email, @RequestParam String code) {
    return authService.verifyCode(email, code);
}
```

Hazırdır! Bu addımları tamamladıqdan sonra sizin sisteminiz SMTP üzərindən 6 rəqəmli OTP kod sistemi ilə tam olaraq mühafizə olunacaq. Uğurlar!
