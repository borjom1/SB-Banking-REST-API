package com.example.banking.repository;

import com.example.banking.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByPhoneNumber(String phoneNumber);

    Optional<UserEntity> findByIpn(String ipn);

}