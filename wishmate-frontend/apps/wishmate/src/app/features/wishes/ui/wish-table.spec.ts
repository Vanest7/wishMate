import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { WishTable } from './wish-table';
import { WishCardModel } from '../models/wish-card.model';

const model: WishCardModel = {
  wish: {
    id: 5,
    eventId: 4,
    userId: 7,
    name: 'Book',
    description: 'Hardcover',
  },
  ownerName: 'Ana',
  isMine: true,
  canManage: true,
  reserved: false,
  purchaseHref: null,
  price: '—',
};
describe('Wish table presentation', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({ providers: [provideRouter([])] }),
  );
  async function setup() {
    const fixture = TestBed.createComponent(WishTable);
    fixture.componentRef.setInput('models', [model]);
    fixture.componentRef.setInput('groupId', 1);
    fixture.componentRef.setInput('eventId', 4);
    fixture.componentRef.setInput('canReserve', true);
    await fixture.whenStable();
    return fixture;
  }
  it('emits edit and delete actions and disables both during pending work', async () => {
    const fixture = await setup();
    const edit = vi.fn(),
      remove = vi.fn();
    fixture.componentInstance.editRequested.subscribe(edit);
    fixture.componentInstance.deleteRequested.subscribe(remove);
    const buttons = (
      fixture.nativeElement as HTMLElement
    ).querySelectorAll<HTMLButtonElement>('button');
    buttons[0].click();
    buttons[1].click();
    expect(edit).toHaveBeenCalledExactlyOnceWith(model.wish);
    expect(remove).toHaveBeenCalledExactlyOnceWith(model.wish);
    fixture.componentRef.setInput('disabled', true);
    await fixture.whenStable();
    buttons[0].click();
    buttons[1].click();
    expect(edit).toHaveBeenCalledExactlyOnceWith(model.wish);
    expect(remove).toHaveBeenCalledExactlyOnceWith(model.wish);
  });
  it('never offers a reservation on an own wish and updates when ownership changes', async () => {
    const fixture = await setup();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('a[href^="/reservations/new"]')).toBeNull();
    fixture.componentRef.setInput('models', [
      {
        ...model,
        isMine: false,
        canManage: false,
      },
    ]);
    await fixture.whenStable();
    expect(
      element.querySelector('a[href="/reservations/new/1/4/5"]'),
    ).not.toBeNull();
    expect(element.querySelector('button')).toBeNull();
  });
  it('shows a disabled reserved button instead of a reservation link', async () => {
    const fixture = await setup();
    fixture.componentRef.setInput('models', [
      {
        ...model,
        isMine: false,
        canManage: false,
        reserved: true,
      },
    ]);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    const button = element.querySelector<HTMLButtonElement>('button');
    expect(button?.textContent).toContain('Regalo reservado');
    expect(button?.disabled).toBe(true);
    expect(element.querySelector('a[href^="/reservations/new"]')).toBeNull();
  });
  it('hides reservation state even if an own model contains a reserved flag', async () => {
    const fixture = await setup();
    fixture.componentRef.setInput('models', [{ ...model, reserved: true }]);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).not.toContain('Regalo reservado');
    expect(element.querySelector('a[href^="/reservations/new"]')).toBeNull();
    expect(element.querySelectorAll('button')).toHaveLength(2);
  });
  it('hides mutation actions for a read-only event', async () => {
    const fixture = await setup();
    fixture.componentRef.setInput('models', [
      { ...model, isMine: false, canManage: false },
    ]);
    fixture.componentRef.setInput('canReserve', false);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('button')).toBeNull();
    expect(element.querySelector('a[href^="/reservations/new"]')).toBeNull();
  });
  it('renders descriptions, absent prices and safe purchase links', async () => {
    const fixture = await setup();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('Hardcover');
    expect(element.textContent).toContain('—');
    fixture.componentRef.setInput('models', [
      { ...model, purchaseHref: 'https://example.com/book' },
    ]);
    await fixture.whenStable();
    const link = element.querySelector<HTMLAnchorElement>('a');
    expect(link?.getAttribute('href')).toBe('https://example.com/book');
    expect(link?.rel).toBe('noopener noreferrer');
  });
});
