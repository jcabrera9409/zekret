import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationService } from './notification.service';
import { Message } from '../_model/message';

@Injectable({
  providedIn: 'root'
})
export class ErrorService {

  constructor(
    private notificationService: NotificationService,
    private router: Router
  ) { }

  handleError(error: HttpErrorResponse): void {
    console.error('An error occurred:', error);
    this.getFriendlyMessage(error);
  }

  private getFriendlyMessage(error: HttpErrorResponse) {
    switch (error.status) {
      case 401: 
        this.router.navigate(['/login']);
        this.notificationService.setMessageChange(
          Message.error(error.error.message || 'No autorizado. Por favor, inicia sesión.')
        );
        break;
      default: 
        this.notificationService.setMessageChange(
          Message.error(error.error.message || 'Ocurrió un error inesperado. Por favor, inténtalo de nuevo más tarde.')
        );
        break;
    }
  }
}
