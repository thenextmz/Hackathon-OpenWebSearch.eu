import { indexIndices } from '@/app/utils/globals';
import { getDisplayString } from '@/app/utils/helper';
import { Result } from '@/types/queryParams';
import { FC } from 'react';

interface IndexHeaderProps {
  indexname: string;
  itemsLength: number;
}

export const IndexHeader: FC<IndexHeaderProps> = ({ indexname, itemsLength }) => {
  return (
    <div className="flex items-center justify-between p-2 bg-theme rounded-t-xl">
      <p className="flex flex-1 justify-start text-white text-1xl text-semibold">{getDisplayString(indexname, indexIndices)}</p>
      <p className="flex flex-1 justify-center text-white text-1xl text-semibold">{'·'}</p>
      <p className="flex flex-1 justify-end text-white text-1xl text-semibold">{itemsLength} Elements</p>
    </div>
  );
};
