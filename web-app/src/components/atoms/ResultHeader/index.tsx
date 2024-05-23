import { FC } from 'react';

interface ResultHeaderProps {
  query: string;
}

export const ResultHeader: FC<ResultHeaderProps> = ({ query }) => {
  return (
    <div className="w-full bg-theme p-2 rounded-xl justify-center">
      <h1 className="font-bold text-white text-center">Results for: "{query}"</h1>
    </div>
  );
};
