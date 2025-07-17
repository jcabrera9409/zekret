import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment.development';
import { User } from '../_model/user';
import { APIResponseDTO, AuthenticationResponseDTO } from '../_model/dto';
import { UtilMethods } from '../util/util';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private url: string = `${environment.apiUrl}/auth`;

  constructor(
    private http: HttpClient,
    private router: Router
  ) { }

  login(email: string, password: string) {
    let user: User = new User(); 
    user.email = email;
    user.username = email;
    user.password = password;
    
    return this.http.post<APIResponseDTO<AuthenticationResponseDTO>>(`${this.url}/login`, user);
  }

  estaLogueado() {
    let token = UtilMethods.getJwtToken();
    return token != null;
  }

  cerrarSesion() {
    this.http.get(`${environment.apiUrl}/logout`).subscribe(() => {
      localStorage.clear();
      this.router.navigate(['login']);
    });
  }
}
