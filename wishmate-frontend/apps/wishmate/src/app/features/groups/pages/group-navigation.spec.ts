import { TestBed } from '@angular/core/testing';
import {
  provideRouter,
  withComponentInputBinding,
  withRouterConfig,
} from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import {
  EventsService,
  GroupsService,
  GroupMembersService,
} from '@wishmate/api-client';
import { of } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { groupsRoutes } from '../groups.routes';
import { GroupDetailPage } from './group-detail-page';

describe('Group section navigation', () => {
  it('keeps the group header and data while switching sections and supports direct links', async () => {
    const getGroup = vi.fn(() => of({ id: 1, name: 'Familia' }));
    const getMembers = vi.fn(() =>
      of([{ userId: 7, userName: 'Alex', role: 'MEMBER' }]),
    );
    TestBed.configureTestingModule({
      providers: [
        provideRouter(
          [{ path: 'groups', children: groupsRoutes }],
          withComponentInputBinding(),
          withRouterConfig({ paramsInheritanceStrategy: 'always' }),
        ),
        { provide: GroupsService, useValue: { getGroup } },
        { provide: EventsService, useValue: { getEvents: () => of([]) } },
        { provide: GroupMembersService, useValue: { getMembers } },
      ],
    });
    const harness = await RouterTestingHarness.create();
    const page = await harness.navigateByUrl(
      '/groups/1/members',
      GroupDetailPage,
    );
    const heading = harness.routeNativeElement?.querySelector('#group-title');
    const navigation = harness.routeNativeElement?.querySelector('nav');
    expect(harness.routeNativeElement?.textContent).toContain('Alex');

    for (const url of ['/groups/1', '/groups/1/events', '/groups/1/members']) {
      const nextPage = await harness.navigateByUrl(url, GroupDetailPage);
      expect(nextPage).toBe(page);
      expect(harness.routeNativeElement?.querySelector('#group-title')).toBe(
        heading,
      );
      expect(harness.routeNativeElement?.querySelector('nav')).toBe(navigation);
      const active = harness.routeNativeElement?.querySelector(
        'nav [aria-current="page"]',
      );
      expect(active?.getAttribute('href')).toBe(url);
    }
    expect(getGroup).toHaveBeenCalledTimes(1);
    expect(getMembers).toHaveBeenCalledTimes(1);
  });
});
