import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  loginForm: FormGroup;
  registerForm: FormGroup;

  isLoginMode: boolean = true;
  isLoading: boolean = false;

  validationError: boolean = false;
  validationMessage: string = '';

  constructor(
    private router: Router,
  ) {
    this.loginForm = new FormGroup({
      email: new FormControl('', [Validators.required]),
      password: new FormControl('', Validators.required)
    });

    this.registerForm = new FormGroup({
      email: new FormControl('', [Validators.required, Validators.email]),
      username: new FormControl('', Validators.required),
      password: new FormControl('', Validators.required)
    });
  }

  toggleAuth() {
    if (this.isLoginMode) {
      if (this.loginForm.valid) {
        this.isLoading = true;
        console.log('Logging in with', this.loginForm.value);
        setTimeout(() => {
          this.isLoading = false;
          console.log('Login successful');
          this.router.navigate(['/', this.loginForm.value.email]);
        }, 2000);
      } else {
        this.validationError = true;
        this.validationMessage = 'Por favor, complete todos los campos requeridos correctamente.';
      }
    }
  }

  toggleRegister() {
    if (!this.isLoginMode) {
      if (this.registerForm.valid) {
        this.isLoading = true;
        console.log('Registering with', this.registerForm.value);
        setTimeout(() => {
          this.isLoading = false;
          console.log('Register successful');
          this.router.navigate(['/space', this.registerForm.value.username]);
        }, 2000);
      } else {
        this.validationError = true;
        this.validationMessage = 'Por favor, complete todos los campos requeridos correctamente.';
      }
    }
  }
}
