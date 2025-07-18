import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { APIResponseDTO } from '../_model/dto';
import { Subject } from 'rxjs';
import { Message } from '../_model/message';

@Injectable({
  providedIn: 'root'
})
export class GenericService<T> {

  private objectChange: Subject<T[]> = new Subject<T[]>();
  private objectDeleteChange: Subject<T> = new Subject<T>();
  private messageChange: Subject<Message> = new Subject<Message>();

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

  modifyByZrn(zrn: string, t: T) {
    return this.http.put<APIResponseDTO<T>>(`${this.url}/${zrn}`, t);
  }

  delete(id: number) {
    return this.http.delete<APIResponseDTO<string>>(`${this.url}/${id}`);
  }

  deleteByZrn(zrn: string) {
    return this.http.delete<APIResponseDTO<string>>(`${this.url}/${zrn}`);
  }

  getChangeObject() {
    return this.objectChange.asObservable();
  }

  setChangeObject(t: T[]) {
    this.objectChange.next(t);
  }

  getChangeObjectDelete() {
    return this.objectDeleteChange.asObservable();
  }

  setChangeObjectDelete(t: T) {
    this.objectDeleteChange.next(t);
  }

  getChangeMessage() {
    return this.messageChange.asObservable();
  }

  setChangeMessage(message: Message) {
    this.messageChange.next(message);
  }
}