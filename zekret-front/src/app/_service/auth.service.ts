import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { User } from '../_model/user';
import { APIResponseDTO, AuthenticationResponseDTO } from '../_model/dto';
import { UtilMethods } from '../util/util';
import { EnvService } from './env.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private url: string = `${this.envService.getApiUrl()}/auth`;

  constructor(
    private http: HttpClient,
    private router: Router,
    private envService: EnvService,
    private utilMethods: UtilMethods
  ) { }

  login(email: string, password: string) {
    let user: User = new User(); 
    user.email = email;
    user.username = email;
    user.password = password;
    
    return this.http.post<APIResponseDTO<AuthenticationResponseDTO>>(`${this.url}/login`, user);
  }

  isLogged() {
    let token = this.utilMethods.getJwtToken();
    return token != null;
  }

  logout() {
    this.http.get(`${this.envService.getApiUrl()}/auth/logout`)
      .subscribe(() => {
        localStorage.clear();
        this.router.navigate(['login']);
      })

  }
}
