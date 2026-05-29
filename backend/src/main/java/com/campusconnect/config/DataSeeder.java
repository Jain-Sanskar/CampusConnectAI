package com.campusconnect.config;

import com.campusconnect.entity.Resource;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import com.campusconnect.repository.ResourceRepository;
import com.campusconnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds a default admin and a handful of sample resources on first run so the app
 * is usable straight away. Everything here is idempotent - it only inserts what's missing.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@campusconnect.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;

    public DataSeeder(UserRepository userRepository,
                      ResourceRepository resourceRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        User admin = seedAdmin();
        seedSampleResources(admin);
    }

    private User seedAdmin() {
        return userRepository.findByEmail(adminEmail).orElseGet(() -> {
            User admin = new User("Campus Admin", adminEmail,
                    passwordEncoder.encode(adminPassword), Role.ADMIN);
            User saved = userRepository.save(admin);
            log.info("Seeded default admin account: {}", adminEmail);
            if ("Admin@123".equals(adminPassword)) {
                log.warn("Default admin password is in use - set ADMIN_PASSWORD before any real deployment.");
            }
            return saved;
        });
    }

    private void seedSampleResources(User admin) {
        if (resourceRepository.count() > 0) {
            return;
        }

        List<Resource> samples = List.of(
                resource("DBMS Previous Year Questions 2023", "Solved question paper from last year's end-sem.",
                        "Previous Year Questions", "DBMS", "https://drive.google.com/sample/dbms-pyq-2023", "PDF", admin.getId()),
                resource("Operating Systems - Complete Notes", "Unit-wise handwritten notes covering the full syllabus.",
                        "Notes", "Operating Systems", "https://drive.google.com/sample/os-notes", "PDF", admin.getId()),
                resource("Data Structures Syllabus", "Official semester syllabus and topic breakdown.",
                        "Syllabus", "Data Structures", "https://college.edu/syllabus/ds", "LINK", admin.getId()),
                resource("Computer Networks PYQ Bundle", "Five years of previous year questions in one file.",
                        "Previous Year Questions", "Computer Networks", "https://drive.google.com/sample/cn-pyq", "PDF", admin.getId())
        );

        resourceRepository.saveAll(samples);
        log.info("Seeded {} sample resources", samples.size());
    }

    private Resource resource(String title, String description, String category,
                              String subject, String url, String type, Long uploadedBy) {
        Resource r = new Resource();
        r.setTitle(title);
        r.setDescription(description);
        r.setCategory(category);
        r.setSubject(subject);
        r.setResourceUrl(url);
        r.setType(type);
        r.setUploadedBy(uploadedBy);
        return r;
    }
}
