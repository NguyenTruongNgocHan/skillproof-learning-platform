export type ContentType = "Quiz" | "Mock Test";
export type ContentSource = "OFFICIAL" | "COMMUNITY";

export interface CommunityContentItem {
  id: string;
  title: string;
  creator: string;
  type: ContentType;
  source: ContentSource;
  topic: string;
  difficulty: "Beginner" | "Intermediate" | "Advanced";
  questions: number;
  rating: number;
  reviewCount: number;
  free: boolean;
  description: string;
}

export const COMMUNITY_CONTENT: CommunityContentItem[] = [
  {
    id: "1",
    title: "Java Backend Interview Practice",
    creator: "Alex Nguyen",
    type: "Quiz",
    source: "COMMUNITY",
    topic: "Programming",
    difficulty: "Intermediate",
    questions: 25,
    rating: 4.7,
    reviewCount: 128,
    free: true,
    description: "",
  },
  {
    id: "2",
    title: "REST API Mock Test",
    creator: "Maria Santos",
    type: "Mock Test",
    source: "COMMUNITY",
    topic: "APIs",
    difficulty: "Intermediate",
    questions: 40,
    rating: 4.5,
    reviewCount: 89,
    free: true,
    description: "",
  },
  {
    id: "3",
    title: "Software Testing Fundamentals",
    creator: "John Lee",
    type: "Quiz",
    source: "COMMUNITY",
    topic: "Testing",
    difficulty: "Beginner",
    questions: 30,
    rating: 4.8,
    reviewCount: 201,
    free: true,
    description: "",
  },
  {
    id: "4",
    title: "Cloud Fundamentals Practice Set",
    creator: "Sarah Kim",
    type: "Mock Test",
    source: "COMMUNITY",
    topic: "Cloud",
    difficulty: "Beginner",
    questions: 35,
    rating: 4.3,
    reviewCount: 67,
    free: false,
    description: "",
  },
  {
    id: "5",
    title: "SQL Query Practice",
    creator: "David Chen",
    type: "Quiz",
    source: "COMMUNITY",
    topic: "Databases",
    difficulty: "Intermediate",
    questions: 20,
    rating: 4.6,
    reviewCount: 156,
    free: true,
    description: "",
  },
  {
    id: "6",
    title: "Python OOP Concepts",
    creator: "Linh Tran",
    type: "Quiz",
    source: "COMMUNITY",
    topic: "Programming",
    difficulty: "Intermediate",
    questions: 25,
    rating: 4.4,
    reviewCount: 94,
    free: true,
    description: "",
  },
  {
    id: "7",
    title: "Docker & Containers Intro",
    creator: "James Park",
    type: "Mock Test",
    source: "COMMUNITY",
    topic: "Cloud",
    difficulty: "Advanced",
    questions: 28,
    rating: 4.5,
    reviewCount: 73,
    free: false,
    description: "",
  },
  {
    id: "8",
    title: "HTTP Protocol Deep Dive",
    creator: "Ana Rodriguez",
    type: "Quiz",
    source: "COMMUNITY",
    topic: "APIs",
    difficulty: "Advanced",
    questions: 20,
    rating: 4.7,
    reviewCount: 112,
    free: true,
    description: "",
  },
];

export const TOPICS = ["All", "Programming", "APIs", "Databases", "Cloud", "Testing"];
