import { ApiResponse, QueryParams, Result } from '@/types/queryParams';
import { FC } from 'react';
import { IndexHeader } from '../atoms/IndexHeader';
import { ISODateString, getDescription } from '@/app/utils/helper';
import Link from 'next/link';
import { MetaDataText } from '../atoms/MetaDataText';
import { LocationText } from '../atoms/LocationText';
import { ChevronRightIcon, SearchIcon } from '@chakra-ui/icons';
import { Button } from '@chakra-ui/react';
import { FullTextSection } from '../molecules/FullTextSection';

interface ResultSectionProps {
  results: ApiResponse;
  queryParams: QueryParams;
}

export const ResultSection: FC<ResultSectionProps> = ({ results, queryParams }) => {
  const indices = Object.keys(results.results).map((key) => Object.keys(results.results[key])[0]);

  return (
    <div className="flex flex-col gap-4 w-full">
      {/** Map Each Index With Results */}
      {indices.map((indexname: any, index) => {
        const items: Result[] = results.results[index][indexname];
        return (
          <div className="bg-white rounded-xl pb-2" key={index}>
            <IndexHeader indexname={indexname} itemsLength={items.length} />

            {/** Map Each Result Item Within an Index */}
            {items.map((item, itemIndex) => {
              return (
                <div key={itemIndex}>
                  <MetaDataText item={item} />
                  <FullTextSection item={item} />
                </div>
              );
            })}
          </div>
        );
      })}
    </div>
  );
};
