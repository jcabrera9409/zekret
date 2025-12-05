import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { APIResponseDTO } from '../_model/dto';
import { Subject } from 'rxjs';
import { Message } from '../_model/message';

@Injectable({
  providedIn: 'root'
})
export class GenericService<TRequest, TResponse> {

  private objectChange: Subject<TResponse[]> = new Subject<TResponse[]>();
  private objectDeleteChange: Subject<TResponse> = new Subject<TResponse>();
  private messageChange: Subject<Message> = new Subject<Message>();

  constructor(
    protected http: HttpClient,
    protected url: String
  ) { }

  getAll() {
    return this.http.get<APIResponseDTO<TResponse[]>>(`${this.url}`);
  }

  getByZrn(zrn: number) {
    return this.http.get<APIResponseDTO<TResponse>>(`${this.url}/${zrn}`);
  }

  register(t: TRequest) {
    return this.http.post<APIResponseDTO<TResponse>>(`${this.url}`, t);
  }

  modify(t: TRequest) {
    return this.http.put<APIResponseDTO<TResponse>>(`${this.url}`, t);
  }

  modifyByZrn(zrn: string, t: TRequest) {
    return this.http.put<APIResponseDTO<TResponse>>(`${this.url}/${zrn}`, t);
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

  setChangeObject(t: TResponse[]) {
    this.objectChange.next(t);
  }

  getChangeObjectDelete() {
    return this.objectDeleteChange.asObservable();
  }

  setChangeObjectDelete(t: TResponse) {
    this.objectDeleteChange.next(t);
  }

  getChangeMessage() {
    return this.messageChange.asObservable();
  }

  setChangeMessage(message: Message) {
    this.messageChange.next(message);
  }
}