import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-cancel-reservation-dialog',
  imports: [MatDialogModule, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2 mat-dialog-title>¿Cancelar tu reserva?</h2>
    <mat-dialog-content
      ><p>Dejarás de encargarte de este regalo:</p>
      <p>
        <strong>{{ name }}</strong>
      </p></mat-dialog-content
    >
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="false">Conservar reserva</button>
      <button mat-flat-button [mat-dialog-close]="true">
        Cancelar reserva
      </button>
    </mat-dialog-actions>
  `,
})
export class CancelReservationDialog {
  readonly name = inject<string>(MAT_DIALOG_DATA);
}
