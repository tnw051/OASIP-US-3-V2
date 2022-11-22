-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema oasip
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `oasip` ;

-- -----------------------------------------------------
-- Schema oasip
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `oasip` DEFAULT CHARACTER SET utf8 ;
USE `oasip` ;

-- -----------------------------------------------------
-- Table `oasip`.`eventCategory`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `oasip`.`eventCategory` (
  `eventCategoryId` INT NOT NULL AUTO_INCREMENT,
  `eventCategoryName` VARCHAR(100) NOT NULL,
  `eventCategoryDescription` VARCHAR(500) NULL,
  `eventDuration` INT NOT NULL,
  PRIMARY KEY (`eventCategoryId`),
  UNIQUE INDEX `eventCategoryName_UNIQUE` (`eventCategoryName` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `oasip`.`user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `oasip`.`user` (
  `userId` INT NOT NULL AUTO_INCREMENT,
  `createdOn` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedOn` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`userId`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `oasip`.`event`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `oasip`.`event` (
  `eventId` INT NOT NULL AUTO_INCREMENT,
  `bookingName` VARCHAR(100) NOT NULL,
  `bookingEmail` VARCHAR(50) NOT NULL,
  `eventStartTime` DATETIME NOT NULL,
  `eventDuration` INT NOT NULL,
  `eventNotes` VARCHAR(500) NULL,
  `bucketUuid` VARCHAR(36) NULL,
  `eventCategoryId` INT NOT NULL,
  `userId` INT NULL,
  PRIMARY KEY (`eventId`),
  INDEX `fk_event_eventCategory_idx` (`eventCategoryId` ASC) VISIBLE,
  INDEX `fk_event_user1_idx` (`userId` ASC) VISIBLE,
  CONSTRAINT `fk_event_eventCategory`
    FOREIGN KEY (`eventCategoryId`)
    REFERENCES `oasip`.`eventCategory` (`eventCategoryId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_event_user1`
    FOREIGN KEY (`userId`)
    REFERENCES `oasip`.`user` (`userId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `oasip`.`profile`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `oasip`.`profile` (
  `profileId` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(50) NOT NULL,
  `role` ENUM('ADMIN', 'LECTURER', 'STUDENT') NOT NULL DEFAULT 'student',
  `password` VARCHAR(100) NOT NULL,
  `createdOn` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedOn` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `userId` INT NOT NULL,
  PRIMARY KEY (`profileId`),
  UNIQUE INDEX `userName_UNIQUE` (`name` ASC) VISIBLE,
  UNIQUE INDEX `userEmail_UNIQUE` (`email` ASC) VISIBLE,
  INDEX `fk_profile_user1_idx` (`userId` ASC) VISIBLE,
  CONSTRAINT `fk_profile_user1`
    FOREIGN KEY (`userId`)
    REFERENCES `oasip`.`user` (`userId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `oasip`.`eventCategoryOwner`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `oasip`.`eventCategoryOwner` (
  `eventCategoryOwnerId` INT NOT NULL AUTO_INCREMENT,
  `eventCategoryId` INT NOT NULL,
  `userId` INT NOT NULL,
  INDEX `fk_user_has_eventCategory_eventCategory1_idx` (`eventCategoryId` ASC) VISIBLE,
  PRIMARY KEY (`eventCategoryOwnerId`),
  INDEX `fk_eventCategoryOwner_user1_idx` (`userId` ASC) VISIBLE,
  CONSTRAINT `fk_user_has_eventCategory_eventCategory1`
    FOREIGN KEY (`eventCategoryId`)
    REFERENCES `oasip`.`eventCategory` (`eventCategoryId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_eventCategoryOwner_user1`
    FOREIGN KEY (`userId`)
    REFERENCES `oasip`.`user` (`userId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `oasip`.`aadUser`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `oasip`.`aadUser` (
  `aadUserId` INT NOT NULL AUTO_INCREMENT,
  `tid` VARCHAR(36) NOT NULL,
  `oid` VARCHAR(36) NOT NULL,
  `createdOn` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedOn` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `userId` INT NOT NULL,
  PRIMARY KEY (`aadUserId`),
  INDEX `fk_profile_user1_idx` (`userId` ASC) VISIBLE,
  CONSTRAINT `fk_profile_user10`
    FOREIGN KEY (`userId`)
    REFERENCES `oasip`.`user` (`userId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
