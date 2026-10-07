import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import {
  GroupMembersService,
  SearchUserResponse,
  UsersService,
} from '@wishmate/api-client';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  finalize,
  map,
  of,
  switchMap,
  tap,
} from 'rxjs';
import { groupsContent } from '../groups.content';
import { groupError } from '../group-utils';

export interface AddMemberDialogData {
  groupId: number;
  memberIds?: number[];
}

@Component({
  selector: 'app-add-member-dialog',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatAutocompleteModule,
  ],
  templateUrl: './add-member-dialog.html',
  styleUrl: './group-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddMemberDialog {
  private readonly data = inject<AddMemberDialogData>(MAT_DIALOG_DATA);
  private readonly ref =
    inject<MatDialogRef<AddMemberDialog, boolean>>(MatDialogRef);
  private readonly membersApi = inject(GroupMembersService);
  private readonly usersApi = inject(UsersService);
  private readonly destroyRef = inject(DestroyRef);
  readonly content = groupsContent;
  readonly pending = signal(false);
  readonly error = signal('');
  readonly searchError = signal('');
  readonly searching = signal(false);
  readonly options = signal<SearchUserResponse[]>([]);
  readonly query = signal('');
  readonly user = new FormControl<SearchUserResponse | string | null>(null, {
    validators: [Validators.required],
  });
  readonly form = new FormGroup({ user: this.user });

  constructor() {
    this.user.valueChanges
      .pipe(
        debounceTime(300),
        map((value) => (typeof value === 'string' ? value.trim() : '')),
        distinctUntilChanged(),
        tap(() => {
          this.searchError.set('');
          this.options.set([]);
          this.query.set('');
        }),
        switchMap((query) => {
          this.query.set(query);
          const normalizedQuery = query.startsWith('@')
            ? query.slice(1)
            : query;
          if (normalizedQuery.length < 2) return of([]);
          this.searching.set(true);
          const searchQuery = `@${normalizedQuery}`;
          return this.usersApi.search({ query: searchQuery }).pipe(
            map((users) =>
              users.filter(
                (candidate) =>
                  candidate.id !== undefined &&
                  !this.data.memberIds?.includes(candidate.id),
              ),
            ),
            catchError((error: unknown) => {
              this.searchError.set(groupError(error));
              return of([]);
            }),
            finalize(() => this.searching.set(false)),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((users) => this.options.set(users));
  }

  displayUser(user: SearchUserResponse | string | null): string {
    return typeof user === 'string' ? user : (user?.userName ?? '');
  }

  cancel(): void {
    if (!this.pending()) this.ref.close();
  }

  submit(): void {
    if (this.pending()) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const selected = this.user.value;
    const userId =
      selected && typeof selected !== 'string' ? selected.id : undefined;
    if (userId === undefined) {
      this.form.markAllAsTouched();
      return;
    }
    this.pending.set(true);
    this.error.set('');
    this.form.disable();
    this.ref.disableClose = true;
    this.membersApi
      .addMember({ groupId: this.data.groupId, body: { userId } })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.pending.set(false);
          this.ref.disableClose = false;
          if (this.form.disabled) this.form.enable();
        }),
      )
      .subscribe({
        next: () => this.ref.close(true),
        error: (error: unknown) => {
          this.form.enable();
          this.error.set(
            error instanceof HttpErrorResponse && error.status === 404
              ? this.content.memberMissing
              : error instanceof HttpErrorResponse && error.status === 409
                ? this.content.memberConflict
                : groupError(error),
          );
          if (error instanceof HttpErrorResponse && error.status === 400) {
            const body: unknown = error.error;
            if (
              body &&
              typeof body === 'object' &&
              'fieldErrors' in body &&
              body.fieldErrors &&
              typeof body.fieldErrors === 'object' &&
              'userId' in body.fieldErrors
            ) {
              this.user.setErrors({ server: true });
              this.user.markAsTouched();
            }
          }
        },
      });
  }
}
