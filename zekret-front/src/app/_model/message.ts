export class Message {
    status: string;
    message: string;
    error: any;

    constructor(status: string, message: string, error?: any) {
        this.status = status;
        this.message = message;
        this.error = error;
    }
}