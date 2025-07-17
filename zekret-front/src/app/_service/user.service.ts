import { Injectable } from '@angular/core';
import { User } from '../_model/user';
import { GenericService } from './generic.service';
import { environment } from '../../environments/environment.development';
import { HttpClient } from '@angular/common/http';
import { APIResponseDTO } from '../_model/dto';

@Injectable({
  providedIn: 'root'
})
export class UserService extends GenericService<User> {

  constructor(protected override http: HttpClient) {
    super(
      http,
      `${environment.apiUrl}/users`
    )
  }

  override register(user: User) {
    return this.http.post<APIResponseDTO<User>>(`${this.url}`, user);
  }

}
