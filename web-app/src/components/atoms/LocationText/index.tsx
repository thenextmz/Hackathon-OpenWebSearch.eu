import { Location, Result } from '@/types/queryParams';
import Link from 'next/link';
import { FC } from 'react';

interface LocationTextProps {
  item: Result;
  location: Location;
  locationIndex: number;
}

export const LocationText: FC<LocationTextProps> = ({ item, location, locationIndex }) => {
  const name = location.locationName;
  const long = location.locationEntries[0].longitude;
  const lat = location.locationEntries[0].latitude;
  const lurl = 'https://www.openstreetmap.org/?mlat=' + lat + '&mlon=' + long + '#map=6/' + lat + '/' + long;

  return (
    <Link className="text-sm" style={{ color: 'blue' }} href={lurl}>
      <span className="mr-1">{name}</span>
      <span className="mr-1">{locationIndex !== item.locations.length - 1 && '·'}</span>
    </Link>
  );
};
