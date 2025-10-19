CREATE TABLE `languages` (
  `id` int NOT NULL AUTO_INCREMENT,
  `experience_level` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `job_seeker_profile` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_languages_job_seeker_profile` (`job_seeker_profile`),
  CONSTRAINT `fk_languages_job_seeker_profile` FOREIGN KEY (`job_seeker_profile`) REFERENCES `job_seeker_profile` (`user_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;