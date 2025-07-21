import { Validators } from "@angular/forms";
import { CredentialTypeDTO } from "./dto";

export const credentialType: { [key: string]: CredentialTypeDTO } = {
    username_password: {
        zrn: 'username_password',
        name: 'Username/Password',
        inputs: [
            {
                formControlName: 'up_username',
                validator: [Validators.required],
            },
            {
                formControlName: 'up_password',
                validator: [Validators.required],
            }
        ]
    },
    ssh_username: {
        zrn: 'ssh_username',
        name: 'SSH Username',
        inputs: [
            {
                formControlName: 'up_username',
                validator: [Validators.required],
            },
            {
                formControlName: 'ssh_public_key',
                validator: [],
            },
            {
                formControlName: 'ssh_private_key',
                validator: [Validators.required],
            }
        ]
    },
    secret_text: {
        zrn: 'secret_text',
        name: 'Secret Text',
        inputs: [
            {
                formControlName: 'secret_text',
                validator: [Validators.required],
            }
        ]
    },
    file: {
        zrn: 'file',
        name: 'File',
        inputs: [
            {
                formControlName: 'file_name',
                validator: [Validators.required, Validators.pattern(/^[a-zA-Z0-9._-]+$/)],
            },
            {
                formControlName: 'file_content',
                validator: [Validators.required],
            }
        ]
    }
}