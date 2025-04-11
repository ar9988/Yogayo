package com.d104.domain.usecase

import com.d104.domain.model.SignUpResult
import com.d104.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
){
    suspend operator fun invoke(id: String, password: String, name:String, nickName:String,profileUri: String) : Flow<Result<SignUpResult>> {
        return authRepository.signUp(id,password,name,nickName,profileUri)
    }
}