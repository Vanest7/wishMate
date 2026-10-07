import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { WishCard } from './wish-card';
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
  purchaseHref: null,
  price: '—',
};
describe('Wish card presentation', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({ providers: [provideRouter([])] }),
  );
  async function setup() {
    const fixture = TestBed.createComponent(WishCard);
    fixture.componentRef.setInput('model', model);
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
    expect(edit).toHaveBeenCalledOnce();
    expect(remove).toHaveBeenCalledOnce();
    fixture.componentRef.setInput('disabled', true);
    await fixture.whenStable();
    buttons[0].click();
    buttons[1].click();
    expect(edit).toHaveBeenCalledOnce();
    expect(remove).toHaveBeenCalledOnce();
  });
  it('never offers a reservation on an own wish and updates when ownership changes', async () => {
    const fixture = await setup();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('a[href^="/reservations/new"]')).toBeNull();
    fixture.componentRef.setInput('model', {
      ...model,
      isMine: false,
      canManage: false,
    });
    await fixture.whenStable();
    expect(
      element.querySelector('a[href="/reservations/new/1/4/5"]'),
    ).not.toBeNull();
    expect(element.querySelector('button')).toBeNull();
  });
});
