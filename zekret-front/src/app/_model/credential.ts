import { CredentialTypeDTO } from "./dto";
import { Namespace } from "./namespace";

export class Credential {
    title: string;
    zrn: string;
    username: string;
    password: string;
    sshPublicKey: string;
    sshPrivateKey: string;
    secretText: string;
    fileName: string;
    fileContent: string;
    notes: string;
    createdAt: Date;
    updatedAt: Date;
    credentialType: CredentialTypeDTO;
    namespace: Namespace
}