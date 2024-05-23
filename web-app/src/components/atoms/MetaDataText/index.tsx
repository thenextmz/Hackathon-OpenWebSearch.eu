import { ISODateString, getDescription } from '@/app/utils/helper';
import { Result } from '@/types/queryParams';
import Link from 'next/link';
import { FC } from 'react';
import { LocationText } from '../LocationText';

interface MetaDataTextProps {
  item: Result;
}

export const MetaDataText: FC<MetaDataTextProps> = ({ item }) => {
  const language = item.language;
  const wordCount = item.wordCount;
  const date = ISODateString(item.warcDate);
  return (
    <div className="bg-white flex flex-col rounded-xl px-2 pt-2">
      {/** Title */}
      <p className="text-black text-xl font-bold">{item.title}</p>

      {/** Access URL */}
      <Link style={{ color: 'blue' }} href={item.url}>
        <p className="text-sm">{item.url}</p>
      </Link>

      {/** Metadata */}
      <div className="flex flex-row gap-1">
        <p className="text-black text-sm font-bold italic">Language:</p>
        <p className="text-black text-sm italic">{language},</p>

        <p className="text-black text-sm font-bold italic">word count:</p>
        <p className="text-black text-sm italic">{wordCount},</p>

        <p className="text-black text-sm font-bold italic">index date:</p>
        <p className="text-black text-sm italic">{date},</p>
      </div>

      {/** Description */}
      <p className="flex flex-1 flex-row flex-wrap items-center">
        <span className="text-black text-sm font-bold italic mr-1">Description: </span>
        <span className="text-black text-sm italic">{getDescription(item.textSnippet)}</span>
      </p>

      {/** Locations */}
      <div className="flex flex-1 flex-row flex-wrap items-center">
        {item.locations.length > 0 && <span className="text-sm font-bold mr-1">Locations:</span>}
        {item.locations.map((location, locationIndex) => (
          <LocationText key={locationIndex} item={item} location={location} locationIndex={locationIndex} />
        ))}
      </div>

      {/** Keywords */}
      <div className="flex flex-1 flex-row flex-wrap items-center">
        {item.keywords.length > 0 && <span className="text-sm font-bold mr-1">Keywords:</span>}
        {item.keywords.map((keyword, keywordIndex) => (
          <p className="text-sm">
            <span className="mr-1">{keyword}</span>
            <span className="mr-1">{keywordIndex !== item.keywords.length - 1 && '·'}</span>
          </p>
        ))}
      </div>
    </div>
  );
};
