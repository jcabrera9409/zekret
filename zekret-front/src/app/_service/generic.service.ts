import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { APIResponseDTO } from '../_model/dto';

@Injectable({
  providedIn: 'root'
})
export class GenericService<T> {

  constructor(
    protected http: HttpClient,
    protected url: String
  ) { }

  getAll() {
    return this.http.get<APIResponseDTO<T[]>>(`${this.url}`);
  }

  getByZrn(zrn: number) {
    return this.http.get<APIResponseDTO<T>>(`${this.url}/${zrn}`);
  }

  register(t: T) {
    return this.http.post<APIResponseDTO<T>>(`${this.url}`, t);
  }

  modify(t: T) {
    return this.http.put<APIResponseDTO<T>>(`${this.url}`, t);
  }

  delete(id: number) {
    return this.http.delete<APIResponseDTO<string>>(`${this.url}/${id}`);
  }
}