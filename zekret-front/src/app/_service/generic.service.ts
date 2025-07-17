import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class GenericService<T> {

  constructor(
    protected http: HttpClient,
    protected url: String
  ) { }

  getAll() {
    return this.http.get<T[]>(`${this.url}`);
  }

  getByZrn(zrn: number) {
    return this.http.get<T>(`${this.url}/${zrn}`);
  }

  register(t: T) {
    return this.http.post(`${this.url}`, t);
  }

  modify(t: T) {
    return this.http.put(`${this.url}`, t);
  }

  delete(id: number) {
    return this.http.delete(`${this.url}/${id}`);
  }
}