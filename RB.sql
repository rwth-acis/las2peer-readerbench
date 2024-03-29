-- MySQL Script generated by MySQL Workbench
-- Sat May  8 22:35:31 2021
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS  `results` ;
DROP TABLE IF EXISTS  `question` ;
DROP TABLE IF EXISTS  `topic` ;
-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------


-- -----------------------------------------------------
-- Table  `topic`
-- -----------------------------------------------------


CREATE TABLE IF NOT EXISTS  `topic` (
  `topic_id` VARCHAR(500) NOT NULL,
  `topic_name` TEXT NOT NULL,
  `processed` BOOLEAN NOT NULL,
  `due_date` TEXT NULL,
  PRIMARY KEY (`topic_id`))
ENGINE = InnoDB  CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


-- -----------------------------------------------------
-- Table  `question`
-- -----------------------------------------------------


CREATE TABLE IF NOT EXISTS  `question` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `question` TEXT NOT NULL,
  `topic_id` TEXT NOT NULL,
  `textref` TEXT NULL,
  `numberOfPoints` DOUBLE NOT NULL,
  PRIMARY KEY (`id`)
    )
ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;




-- -----------------------------------------------------
-- Table  `results`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS  `results` (
  `result_id` INT NOT NULL AUTO_INCREMENT,
  `filename` TEXT NOT NULL,
  PRIMARY KEY (`result_id`)
    )
ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS=1;
