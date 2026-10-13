import { ValidWish } from '../wish-utils';

export interface WishCardModel {
  wish: ValidWish;
  ownerName: string;
  isMine: boolean;
  canManage: boolean;
  reserved: boolean;
  purchaseHref: string | null;
  price: string;
}
