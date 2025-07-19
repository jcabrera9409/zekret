import { ValidatorFn } from "@angular/forms";

export interface APIResponseDTO<T> {
    statusCode: number;
    message: string;
    success: boolean;
    timestamp: string;
    data: T;
}

export interface AuthenticationResponseDTO {
    access_token: string;
    refresh_token: string;
    message: number;
}

export interface CredentialTypeDTO {
    zrn: string;
    name: string;
    inputs: { formControlName: string; validator?: ValidatorFn[] }[];
}

export interface ConfirmDeleteDataDTO {
  title?: string;
  message?: string;
  confirmText?: string;
  cancelText?: string;
}