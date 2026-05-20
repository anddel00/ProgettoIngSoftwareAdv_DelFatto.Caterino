package com.ProgettoISA.WMS.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequestDTO {
    
    @NotBlank(message = "L'email non può essere vuota")
    @Email(message = "Formato email non valido")
    private String email;
    
    @NotBlank(message = "La password non può essere vuota")
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
