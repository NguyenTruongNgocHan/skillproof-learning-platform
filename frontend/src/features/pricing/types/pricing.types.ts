export interface Sale {
  state: string;
  accessMode: string;
  learningPrice: number;
  certificationPrice: number;
}
export interface PriceChange {
  id: string;
  actorId: string;
  accessMode: string;
  previousPrice: number;
  priceVnd: number;
  previousCertificationPrice: number;
  certificationPriceVnd: number;
  reason: string;
  createdAt: string;
}
export interface Promotion {
  id: string;
  name: string;
  percent: number;
  startsAt: string;
  endsAt: string;
  active: boolean;
}
export interface Commercial {
  offer: Sale;
  baseOffer: Sale;
  history: PriceChange[];
  promotions: Promotion[];
}
