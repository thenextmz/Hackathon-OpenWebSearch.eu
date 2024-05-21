import { Result } from '@/components/molecules/Result';
import { ApiResponse, QueryParams } from '@/types/queryParams';
import ApiClient from '@/app/utils/api';

export default async function Home() {
  const api = new ApiClient();
  const queryParams: QueryParams = {
    query: 'Graz',
  };
  const data: ApiResponse = await api.search(queryParams);
  return (
    <main className="bg-themeDark p-24 px-60 flex flex-col min-h-screen gap-4 items-center">
      <div className="w-full bg-theme p-4 rounded-md justify-center">
        <h1 className="text-xl font-bold text-white text-center">Results for: {queryParams.query}</h1>
      </div>
      <Result results={data} queryParams={queryParams} />
    </main>
  );
}

// flex min-h-screen flex-col items-center justify-between p-24
