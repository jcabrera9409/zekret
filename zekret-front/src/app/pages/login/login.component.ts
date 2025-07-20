import { Component, Renderer2 } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../_service/auth.service';
import { UtilMethods } from '../../util/util';
import { LoaderComponent } from "../../shared/loader/loader.component";
import { Message } from '../../_model/message';
import { NotificationService } from '../../_service/notification.service';
import { finalize } from 'rxjs';
import { FormMethods } from '../../util/forms';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, LoaderComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  loginForm: FormGroup;
  registerForm: FormGroup;

  isLoginMode: boolean = true;
  isLoading: boolean = false;

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService,
    private renderer: Renderer2,
    private router: Router,
    private utilMethods: UtilMethods
  ) {
    this.loginForm = new FormGroup({
      email: new FormControl('', [Validators.required]),
      password: new FormControl('', [Validators.required, Validators.minLength(6)])
    });

    this.registerForm = new FormGroup({
      email: new FormControl('', [Validators.required, Validators.email]),
      username: new FormControl('', Validators.required),
      password: new FormControl('', [Validators.required, Validators.minLength(6)])
    });

    FormMethods.addSubscribesForm(this.loginForm, renderer);
    FormMethods.addSubscribesForm(this.registerForm, renderer);
  }

  toggleAuth() {
    if (this.isLoginMode) {
      if (this.loginForm.invalid) {
        FormMethods.validateForm(this.loginForm, this.renderer);
        this.notificationService.setMessageChange(
          Message.error('Por favor, complete todos los campos requeridos correctamente.')
        );
        return;
      }
      this.isLoading = true;
      const email = this.loginForm.value.email;
      const password = this.loginForm.value.password;
      this.authService.login(email, password)
        .pipe(
          finalize(() => {
            this.isLoading = false;
          })
        )
        .subscribe({
          next: (response) => {
            this.utilMethods.setJwtToken(response.data.access_token);
            this.router.navigate([this.utilMethods.getUsernameFieldJwtToken()]);
          }
        });

    }
  }

  toggleRegister() {
    if (!this.isLoginMode) {
      if (this.registerForm.invalid) {
        FormMethods.validateForm(this.registerForm, this.renderer);
        this.notificationService.setMessageChange(
          Message.error('Por favor, complete todos los campos requeridos correctamente.')
        );
        return;
      }
      this.isLoading = true;
      console.log('Registering with', this.registerForm.value);
      setTimeout(() => {
        this.isLoading = false;
        console.log('Register successful');
        this.router.navigate(['/space', this.registerForm.value.username]);
      }, 2000);
    }
  }
}
