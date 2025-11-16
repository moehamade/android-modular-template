package com.example.data.mapper

import com.example.data.local.entity.UserEntity
import com.example.network.dto.UserDto
import com.example.domain.model.User

/**
 * Mapper functions to convert between User representations across layers.
 */


/**
 * Convert domain User model to Room UserEntity
 */
fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        username = username,
        profileImageUrl = profileImageUrl,
        isGuest = isGuest,
        createdAt = createdAt,
        lastLoginAt = lastLoginAt
    )
}


/**
 * Convert Room UserEntity to domain User model
 */
fun UserEntity.toDomain(): User {
    return User(
        id = id,
        email = email,
        username = username,
        profileImageUrl = profileImageUrl,
        isGuest = isGuest,
        createdAt = createdAt,
        lastLoginAt = lastLoginAt
    )
}

/**
 * Convert list of UserEntity to list of domain User models
 */
fun List<UserEntity>.toDomain(): List<User> {
    return map { it.toDomain() }
}

// ===== DTO to Entity (for caching API responses) =====

/**
 * Convert API UserDto to Room UserEntity
 */
fun UserDto.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        username = username,
        profileImageUrl = profileImageUrl,
        isGuest = isGuest,
        createdAt = createdAt,
        lastLoginAt = lastLoginAt
    )
}

/**
 * Convert list of UserDto to list of UserEntity
 */
fun List<UserDto>.toEntityList(): List<UserEntity> {
    return map { it.toEntity() }
}

// ===== DTO to Domain (direct conversion from API) =====

/**
 * Convert API UserDto to domain User model
 */
fun UserDto.toDomain(): User {
    return User(
        id = id,
        email = email,
        username = username,
        profileImageUrl = profileImageUrl,
        isGuest = isGuest,
        createdAt = createdAt,
        lastLoginAt = lastLoginAt
    )
}

/**
 * Convert list of UserDto to list of domain User models
 */
fun List<UserDto>.toDomainList(): List<User> {
    return map { it.toDomain() }
}

// ===== Domain to DTO (for API requests - rare, usually use request DTOs) =====

/**
 * Convert domain User to API UserDto (if needed for API requests)
 */
fun User.toDto(): UserDto {
    return UserDto(
        id = id,
        email = email,
        username = username,
        profileImageUrl = profileImageUrl,
        isGuest = isGuest,
        createdAt = createdAt,
        lastLoginAt = lastLoginAt
    )
}
