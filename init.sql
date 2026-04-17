-- ─────────────────────────────────────────────────────────────
--  Dreamify Database Init Script
--  Runs automatically when MySQL container starts for first time
-- ─────────────────────────────────────────────────────────────

CREATE DATABASE IF NOT EXISTS dreamify
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE dreamify;

-- ── ENQUIRIES TABLE ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS enquiries (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100),
    email       VARCHAR(255) NOT NULL,
    phone       VARCHAR(20),
    company     VARCHAR(200),
    service     VARCHAR(100) NOT NULL,
    budget      VARCHAR(50),
    description TEXT,
    status      ENUM('NEW','CONTACTED','COMPLETED') NOT NULL DEFAULT 'NEW',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_email  (email),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── TESTIMONIALS TABLE ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS testimonials (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_name VARCHAR(150) NOT NULL,
    client_role VARCHAR(200),
    content     TEXT NOT NULL,
    rating      TINYINT NOT NULL DEFAULT 5 CHECK (rating BETWEEN 1 AND 5),
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── SEED: SAMPLE TESTIMONIALS ──────────────────────────────────
INSERT INTO testimonials (client_name, client_role, content, rating) VALUES
(
    'Amit Singh',
    'CEO, TechStart Delhi',
    'Dreamify built our entire SaaS platform in 6 weeks. The quality and attention to detail was outstanding — far beyond what we expected. The team was responsive, professional, and delivered on every promise.',
    5
),
(
    'Priya Nair',
    'Founder, StyleBox',
    'Our React Native app launched with a 4.9-star rating on the Play Store. The Dreamify team nailed our vision perfectly and delivered a flawless user experience. Highly recommend!',
    5
),
(
    'Rohit Mehta',
    'CTO, LogiPrime',
    'Their AWS architecture cut our server costs by 40% while tripling performance. The DevOps pipeline they set up is rock solid. Best cloud team in the country.',
    5
);

-- ── SEED: SAMPLE ENQUIRY ───────────────────────────────────────
INSERT INTO enquiries (first_name, last_name, email, phone, company, service, budget, description, status) VALUES
(
    'Vikram',
    'Sharma',
    'vikram@examplecorp.in',
    '9876543210',
    'ExampleCorp',
    'Web Development',
    '₹50K–₹2L',
    'We need a corporate website with a product catalogue, contact form, and CMS integration.',
    'NEW'
);
