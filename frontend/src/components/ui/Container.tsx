import { ReactNode, ElementType } from 'react';

interface ContainerProps {
  children: ReactNode;
  className?: string;
  as?: ElementType;
}

export default function Container({ children, className = '', as: Tag = 'div' }: ContainerProps) {
  return (
    <Tag className={`max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8 ${className}`}>
      {children}
    </Tag>
  );
}
