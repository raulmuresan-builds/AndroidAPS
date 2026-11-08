# Protectie AAPS Master (activare legata de telefon)

Folder propriu raulmuresan-builds. Nu face parte din AAPS oficial, deci Sync fork nu intra in conflict cu el.

- `ActivationGate.kt` - calculul si verificarea codului.
- `ActivationActivity.kt` - ecranul de activare (ID telefon + camp pentru cod).
- `aplica_protectie.py` - aplica fisierele peste tag-ul oficial in timpul build-ului.
- Workflow: `.github/workflows/aaps-protejat.yml` (doar fullRelease), urca in Google Drive: `AAPS Clienti/Master/<versiune>/`.

Cod = primele 8 caractere (hex, majuscule) din SHA-256("<cheie>:<id>"), id = ID-ul afisat, cu litere mici, fara cratime.
Cheia: secretul `AAPS_ACTIVATION_SECRET` (copie pe Mac: Documents/AAPS_Activare).
