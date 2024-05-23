'use client';

import { ApiResponse, QueryParams } from '@/types/queryParams';
import ApiClient from '@/app/utils/api';
import { QuerySection } from '@/components/organism/QuerySection';
import { useEffect, useState } from 'react';
import { ResultHeader } from '@/components/atoms/ResultHeader';
import { ResultSection } from '@/components/organism/ResultSection';
import { Footer } from '@/components/atoms/Footer';

export default function Home() {
  const api = new ApiClient();
  const [loading, setLoading] = useState<boolean>(false);
  const [data, setData] = useState<ApiResponse | null>(null);
  const [query, setQuery] = useState<string>(''); // Query state for displaying the search query
  const [queryParams, setQueryParams] = useState<QueryParams>({ query: '' }); // Query params for sending the request

  const sendRequest = async () => {
    setData(null);
    setLoading(true);
    const data: ApiResponse = await api.search(queryParams);
    setData(data);
    setLoading(false);
  };

  useEffect(() => {
    console.log(queryParams);
  }, [queryParams]);

  return (
    <main className="flex flex-col flex-1 bg-themeDark min-h-screen">
      <div className="flex flex-col flex-1 p-24 px-60  gap-2 items-center">
        <QuerySection loading={loading} setLoading={setLoading} sendRequest={sendRequest} queryParams={queryParams} setQueryParams={setQueryParams} setQuery={setQuery} />
        {data && <ResultHeader query={query} />}
        {data && <ResultSection results={data} queryParams={queryParams} />}
      </div>
      <Footer />
    </main>
  );
}
