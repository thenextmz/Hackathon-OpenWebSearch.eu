'use client';

import { FC, useEffect, useState } from 'react';
import { SearchBar } from '../molecules/SearchBar';
import { QueryParams, SortBy } from '@/types/queryParams';
import { IndexSelection } from '../molecules/IndexSelection';
import { LanguageSelection } from '../molecules/LanguageSelection';
import { LimitSelection } from '../molecules/LimitSelection';
import Image from 'next/image';
import { Button, Checkbox, CheckboxGroup, Stack } from '@chakra-ui/react';
import { ArrowDownIcon, ArrowUpIcon } from '@chakra-ui/icons';

interface QuerySectionProps {
  loading: boolean;
  setLoading: (loading: boolean) => void;
  sendRequest: () => void;

  setQuery: (query: string) => void;
  queryParams: QueryParams;
  setQueryParams: (queryParams: QueryParams) => void;
}

export const QuerySection: FC<QuerySectionProps> = ({ loading, setLoading, sendRequest, queryParams, setQueryParams, setQuery }) => {
  useEffect(() => {
    console.log('SortBy changed', queryParams.sortby, queryParams.ranking);
  }, [queryParams.sortby, queryParams.ranking]);

  return (
    <div className="flex flex-col w-full items-center">
      <Image alt="MOSAIC" src="/mosaic.jpeg" width="0" height="0" sizes="100vw" className="mb-2 rounded-sm w-[500px] h-auto" />
      <SearchBar queryParams={queryParams} setQueryParams={setQueryParams} setQuery={setQuery} loading={loading} sendRequest={sendRequest} />
      <div className="flex flex-row flex-wrap gap-2 w-full pt-2">
        <IndexSelection queryParams={queryParams} setQueryParams={setQueryParams} />
        <LanguageSelection queryParams={queryParams} setQueryParams={setQueryParams} />
        <LimitSelection queryParams={queryParams} setQueryParams={setQueryParams} />
      </div>

      <div className="rounded-xl flex flex-row flex-wrap gap-2 mt-2 w-full items-center bg-white justify-center">
        {queryParams.sortby && (
          <Button
            className="m-1"
            size={'xs'}
            onClick={() => {
              const oldParams = queryParams;
              setQueryParams({ ...oldParams, ranking: oldParams.ranking == 'asc' ? 'desc' : 'asc' });
            }}
          >
            {queryParams.ranking == 'asc' ? <ArrowUpIcon className="bg-green-200 rounded-xs" /> : <ArrowDownIcon className="bg-green-200 rounded-xs" />}
          </Button>
        )}

        {/** TODO: FIX LOGIC */}
        <CheckboxGroup colorScheme="green" value={[queryParams.sortby!]}>
          <Stack spacing={[1, 5]} direction={['column', 'row']}>
            <Checkbox
              value="length"
              onChange={() => {
                const oldParams = queryParams;
                setQueryParams({ ...oldParams, sortby: oldParams.sortby == 'length' ? undefined : 'length' });
              }}
            >
              Length
            </Checkbox>
            <Checkbox
              value="date"
              onChange={() => {
                const oldParams = queryParams;
                setQueryParams({ ...oldParams, sortby: oldParams.sortby == 'date' ? undefined : 'date' });
              }}
            >
              Date
            </Checkbox>
          </Stack>
        </CheckboxGroup>
      </div>
    </div>
  );
};
