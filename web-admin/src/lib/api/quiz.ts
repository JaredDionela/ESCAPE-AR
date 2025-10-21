import { supabase } from '../supabase';
import { QuizQuestion } from '../../types/database.types';

export async function getAllQuizQuestions(): Promise<QuizQuestion[]> {
  const { data, error } = await supabase
    .from('quiz_questions')
    .select('*')
    .order('module_id', { ascending: true })
    .order('order_index', { ascending: true });

  if (error) throw error;
  return data || [];
}

export async function getQuizQuestionsByModule(module_id: string): Promise<QuizQuestion[]> {
  const { data, error } = await supabase
    .from('quiz_questions')
    .select('*')
    .eq('module_id', module_id)
    .order('order_index', { ascending: true });

  if (error) throw error;
  return data || [];
}

export async function getQuizQuestion(id: string): Promise<QuizQuestion | null> {
  const { data, error } = await supabase
    .from('quiz_questions')
    .select('*')
    .eq('id', id)
    .single();

  if (error) throw error;
  return data;
}

export async function createQuizQuestion(question: Omit<QuizQuestion, 'id' | 'created_at'>): Promise<QuizQuestion> {
  try {
    console.log('Creating quiz question:', question);
    
    // Validate the question data
    if (!question.module_id || !question.question_text || !question.correct_answer) {
      throw new Error('Missing required fields: module_id, question_text, or correct_answer');
    }
    
    if (!['A', 'B', 'C', 'D'].includes(question.correct_answer)) {
      throw new Error('correct_answer must be A, B, C, or D');
    }
    
    const questionData = {
      module_id: question.module_id,
      question_text: question.question_text,
      option_a: question.option_a || '',
      option_b: question.option_b || '',
      option_c: question.option_c || '',
      option_d: question.option_d || '',
      correct_answer: question.correct_answer,
      order_index: question.order_index || 0
    };
    
    const { data, error } = await supabase
      .from('quiz_questions')
      .insert([questionData])
      .select()
      .single();

    if (error) {
      console.error('Supabase error creating quiz question:', error);
      throw new Error(`Failed to create quiz question: ${error.message}`);
    }
    
    console.log('Quiz question created successfully:', data);
    return data;
  } catch (err) {
    console.error('Error in createQuizQuestion:', err);
    throw err;
  }
}

export async function updateQuizQuestion(id: string, updates: Partial<QuizQuestion>): Promise<QuizQuestion> {
  const { data, error } = await supabase
    .from('quiz_questions')
    .update(updates)
    .eq('id', id)
    .select()
    .single();

  if (error) throw error;
  return data;
}

export async function deleteQuizQuestion(id: string): Promise<void> {
  const { error } = await supabase
    .from('quiz_questions')
    .delete()
    .eq('id', id);

  if (error) throw error;
}

// Bulk operations
export async function createMultipleQuestions(questions: Omit<QuizQuestion, 'id' | 'created_at'>[]): Promise<QuizQuestion[]> {
  const { data, error } = await supabase
    .from('quiz_questions')
    .insert(questions)
    .select();

  if (error) throw error;
  return data || [];
}

export async function deleteQuestionsByModule(module_id: string): Promise<void> {
  const { error } = await supabase
    .from('quiz_questions')
    .delete()
    .eq('module_id', module_id);

  if (error) throw error;
}
