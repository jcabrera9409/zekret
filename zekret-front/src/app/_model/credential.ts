import { CredentialTypeDTO } from "./dto";
import { NamespaceRequestDTO, NamespaceResponseDTO } from "./namespace";

export class CredentialResponseDTO {
    zrn: string;
    title: string;
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
    namespace: NamespaceResponseDTO;
}

export class CredentialRequestDTO {
    title: string;
    username: string;
    password: string;
    sshPublicKey: string;
    sshPrivateKey: string;
    secretText: string;
    fileName: string;
    fileContent: string;
    notes: string;
    credentialTypeZrn: string;
    namespaceZrn: string;
}