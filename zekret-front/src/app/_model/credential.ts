import { CredentialTypeDTO } from "./dto";
import { Namespace } from "./namespace";

export class Credential {
    title: string;
    zrn: string;
    username: string;
    password: string;
    sshPrivateKey: string;
    secretText: string;
    fileContent: string;
    notes: string;
    createdAt: Date;
    updatedAt: Date;
    credentialType: CredentialTypeDTO;
    namespace: Namespace
}