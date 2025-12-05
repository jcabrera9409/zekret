import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
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
    return this.http.get<APIResponseDTO<AuthenticationResponseDTO>>(`${this.url}/login?username=${email}&password=${password}`);
  }

  isLogged() {
    let token = this.utilMethods.getJwtToken();
    return token != null;
  }

  logout() {
    this.http.get(`${this.envService.getApiUrl()}/auth/logout`)
      .subscribe(() => {
        sessionStorage.clear();
        this.router.navigate(['login']);
      })

  }
}
