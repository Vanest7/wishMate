import { ValidWish, WishEvent } from '../wish-utils';

export type WishDialogData =
  | { mode: 'create'; groupId: number; event: WishEvent }
  | {
      mode: 'edit' | 'delete';
      groupId: number;
      event: WishEvent;
      wish: ValidWish;
    };
export type WishDialogResult =
  | { mode: 'create' | 'edit'; wish: ValidWish }
  | { mode: 'delete'; wishId: number };

export type WishFormDialogData =
  | { mode: 'create'; groupId: number; event: WishEvent }
  | { mode: 'edit'; groupId: number; event: WishEvent; wish: ValidWish };
export type DeleteWishDialogData = {
  mode: 'delete';
  groupId: number;
  event: WishEvent;
  wish: ValidWish;
};
