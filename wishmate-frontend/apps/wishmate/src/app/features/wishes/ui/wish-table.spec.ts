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
    ).querySelectorAll<HTMLButtonElement>('.actions button');
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
    expect(element.querySelector('.actions button')).toBeNull();
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
    const button = element.querySelector<HTMLButtonElement>('.actions button');
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
    expect(element.querySelectorAll('.actions button')).toHaveLength(2);
  });
  it('hides mutation actions for a read-only event', async () => {
    const fixture = await setup();
    fixture.componentRef.setInput('models', [
      { ...model, isMine: false, canManage: false },
    ]);
    fixture.componentRef.setInput('canReserve', false);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('.actions button')).toBeNull();
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
  it('sorts numeric prices before pagination and keeps missing prices last', async () => {
    const fixture = await setup();
    const models = [100, 9, 0, undefined].map((price, index) => ({
      ...model,
      wish: { ...model.wish, id: index + 1, estimatedPrice: price },
    }));
    fixture.componentRef.setInput('models', models);
    fixture.componentInstance.pageSize.set(2);
    fixture.componentInstance.changeSort({ active: 'price', direction: 'asc' });
    await fixture.whenStable();
    expect(
      fixture.componentInstance
        .visibleModels()
        .map((item) => item.wish.estimatedPrice),
    ).toEqual([0, 9]);
    fixture.componentInstance.changeSort({
      active: 'price',
      direction: 'desc',
    });
    await fixture.whenStable();
    expect(
      fixture.componentInstance
        .sortedModels()
        .map((item) => item.wish.estimatedPrice),
    ).toEqual([100, 9, 0, undefined]);
    expect(models.map((item) => item.wish.estimatedPrice)).toEqual([
      100,
      9,
      0,
      undefined,
    ]);
  });
  it('pages through rows and resets when filtered data changes', async () => {
    const fixture = await setup();
    const models = Array.from({ length: 12 }, (_, index) => ({
      ...model,
      wish: { ...model.wish, id: index + 1, name: `Book ${index + 1}` },
    }));
    fixture.componentRef.setInput('models', models);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelectorAll('tbody tr')).toHaveLength(10);
    element
      .querySelector<HTMLButtonElement>('button[aria-label="Página siguiente"]')
      ?.click();
    await fixture.whenStable();
    expect(element.querySelectorAll('tbody tr')).toHaveLength(2);
    expect(element.querySelector('tbody')?.textContent).toContain('Book 11');
    fixture.componentRef.setInput('models', models.slice(0, 3));
    await fixture.whenStable();
    expect(fixture.componentInstance.pageIndex()).toBe(0);
    expect(element.querySelectorAll('tbody tr')).toHaveLength(3);
  });
  it('sorts names through the header and restores the original order when cleared', async () => {
    const fixture = await setup();
    const models = ['Zorro', 'Árbol', 'Barco'].map((name, index) => ({
      ...model,
      wish: { ...model.wish, id: index + 1, name },
    }));
    fixture.componentRef.setInput('models', models);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    element
      .querySelector<HTMLElement>(
        'th[mat-sort-header="name"] .mat-sort-header-container',
      )
      ?.click();
    await fixture.whenStable();
    expect(
      fixture.componentInstance.visibleModels().map((item) => item.wish.name),
    ).toEqual(['Árbol', 'Barco', 'Zorro']);
    fixture.componentInstance.changeSort({ active: 'name', direction: '' });
    await fixture.whenStable();
    expect(fixture.componentInstance.visibleModels()).toEqual(models);
  });
  it('sorts owners and resets the page when changing the order', async () => {
    const fixture = await setup();
    fixture.componentRef.setInput(
      'models',
      ['Zoe', 'Ana'].map((ownerName, index) => ({
        ...model,
        ownerName,
        wish: { ...model.wish, id: index + 1 },
      })),
    );
    await fixture.whenStable();
    fixture.componentInstance.changePage({
      pageIndex: 1,
      pageSize: 1,
      length: 2,
    });
    fixture.componentInstance.changeSort({ active: 'owner', direction: 'asc' });
    await fixture.whenStable();
    expect(fixture.componentInstance.pageIndex()).toBe(0);
    expect(fixture.componentInstance.visibleModels()[0].ownerName).toBe('Ana');
  });
});
