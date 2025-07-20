import { Injectable } from '@angular/core';
import { User } from '../_model/user';
import { GenericService } from './generic.service';
import { HttpClient } from '@angular/common/http';
import { APIResponseDTO } from '../_model/dto';
import { EnvService } from './env.service';

@Injectable({
  providedIn: 'root'
})
export class UserService extends GenericService<User> {

  constructor(
    protected override http: HttpClient,
    private envService: EnvService
  ) {
    const apiUrl = envService.getApiUrl();
    super(
      http,
      `${apiUrl}/users`
    )
  }

  override register(user: User) {
    return this.http.post<APIResponseDTO<User>>(`${this.url}`, user);
  }

}
