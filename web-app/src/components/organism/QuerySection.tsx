'use client';

import { FC, useState } from 'react';
import { SearchBar } from '../molecules/SearchBar';
import { QueryParams } from '@/types/queryParams';
import { IndexSelection } from '../molecules/IndexSelection';
import { LanguageSelection } from '../molecules/LanguageSelection';
import { LimitSelection } from '../molecules/LimitSelection';
import Image from 'next/image';

interface QuerySectionProps {
  loading: boolean;
  setLoading: (loading: boolean) => void;
  sendRequest: () => void;

  setQuery: (query: string) => void;
  queryParams: QueryParams;
  setQueryParams: (queryParams: QueryParams) => void;
}

export const QuerySection: FC<QuerySectionProps> = ({ loading, setLoading, sendRequest, queryParams, setQueryParams, setQuery }) => {
  return (
    <div className="flex flex-col w-full items-center">
      <Image alt="MOSAIC" src="/mosaic.jpeg" width="0" height="0" sizes="100vw" className="mb-2 rounded-sm w-[500px] h-auto" />
      <SearchBar queryParams={queryParams} setQueryParams={setQueryParams} setQuery={setQuery} loading={loading} sendRequest={sendRequest} />
      <div className="flex flex-row flex-wrap gap-2 w-full pt-2">
        <IndexSelection queryParams={queryParams} setQueryParams={setQueryParams} />
        <LanguageSelection queryParams={queryParams} setQueryParams={setQueryParams} />
        <LimitSelection queryParams={queryParams} setQueryParams={setQueryParams} />
      </div>
    </div>
  );
};
