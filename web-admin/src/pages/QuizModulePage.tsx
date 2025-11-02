import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Paper,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  IconButton,
  CircularProgress,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Radio,
  RadioGroup,
  FormControlLabel,
  FormControl,
  FormLabel,
  Divider,
  Breadcrumbs,
  Link,
} from '@mui/material';
import {
  ArrowBack as BackIcon,
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  ContentCopy as CopyIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  getQuizQuestionsByModule,
  createQuizQuestion,
  updateQuizQuestion,
  deleteQuizQuestion,
} from '../lib/api/quiz';
import { QuizQuestion } from '../types/database.types';

const MODULE_INFO: Record<string, { title: string; color: string }> = {
  decantation: { title: 'Decantation', color: '#2196F3' },
  organ_system: { title: 'Organ System', color: '#4CAF50' },
  simple_machines: { title: 'Simple Machines', color: '#FF9800' },
  solar_system: { title: 'Solar System', color: '#9C27B0' },
};

interface QuizFormData {
  question_text: string;
  option_a: string;
  option_b: string;
  option_c: string;
  option_d: string;
  correct_answer: 'A' | 'B' | 'C' | 'D';
  order_index: number;
}

export default function QuizModulePage() {
  const { moduleId } = useParams<{ moduleId: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [formOpen, setFormOpen] = useState(false);
  const [editingQuestion, setEditingQuestion] = useState<QuizQuestion | null>(null);
  const [formData, setFormData] = useState<QuizFormData>({
    question_text: '',
    option_a: '',
    option_b: '',
    option_c: '',
    option_d: '',
    correct_answer: 'A',
    order_index: 1,
  });

  const moduleInfo = MODULE_INFO[moduleId || ''];

  const {
    data: questions = [],
    isLoading,
    error,
  } = useQuery<QuizQuestion[]>({
    queryKey: ['quiz-questions', moduleId],
    queryFn: () => getQuizQuestionsByModule(moduleId || ''),
    enabled: !!moduleId,
  });

  const createMutation = useMutation({
    mutationFn: createQuizQuestion,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['quiz-questions'] });
      handleCloseForm();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, updates }: { id: string; updates: Partial<QuizQuestion> }) =>
      updateQuizQuestion(id, updates),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['quiz-questions'] });
      handleCloseForm();
    },
  });

  const deleteMutation = useMutation<void, Error, string>({
    mutationFn: deleteQuizQuestion,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['quiz-questions'] });
    },
  });

  const handleOpenForm = (question?: QuizQuestion) => {
    if (question) {
      setEditingQuestion(question);
      setFormData({
        question_text: question.question_text,
        option_a: question.option_a,
        option_b: question.option_b,
        option_c: question.option_c,
        option_d: question.option_d,
        correct_answer: question.correct_answer as 'A' | 'B' | 'C' | 'D',
        order_index: question.order_index,
      });
    } else {
      setEditingQuestion(null);
      setFormData({
        question_text: '',
        option_a: '',
        option_b: '',
        option_c: '',
        option_d: '',
        correct_answer: 'A',
        order_index: questions.length + 1,
      });
    }
    setFormOpen(true);
  };

  const handleCloseForm = () => {
    setFormOpen(false);
    setEditingQuestion(null);
    setFormData({
      question_text: '',
      option_a: '',
      option_b: '',
      option_c: '',
      option_d: '',
      correct_answer: 'A',
      order_index: 1,
    });
  };

  const handleSubmit = () => {
    if (editingQuestion) {
      updateMutation.mutate({ id: editingQuestion.id, updates: formData });
    } else {
      const newQuestion = {
        module_id: moduleId || '',
        ...formData,
      };
      createMutation.mutate(newQuestion as any);
    }
  };

  const handleDelete = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this question?')) {
      deleteMutation.mutate(id);
    }
  };

  const handleDuplicate = (question: QuizQuestion) => {
    setFormData({
      question_text: question.question_text,
      option_a: question.option_a,
      option_b: question.option_b,
      option_c: question.option_c,
      option_d: question.option_d,
      correct_answer: question.correct_answer as 'A' | 'B' | 'C' | 'D',
      order_index: questions.length + 1,
    });
    setEditingQuestion(null);
    setFormOpen(true);
  };

  if (!moduleInfo) {
    return (
      <Box>
        <Alert severity="error">Invalid module ID</Alert>
      </Box>
    );
  }

  return (
    <Box>
      {/* Header with Breadcrumbs */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Breadcrumbs sx={{ mb: 2 }}>
          <Link
            component="button"
            variant="body1"
            onClick={() => navigate('/admin/quiz')}
            sx={{ textDecoration: 'none', cursor: 'pointer' }}
          >
            Quiz Management
          </Link>
          <Typography color="text.primary">{moduleInfo.title}</Typography>
        </Breadcrumbs>

        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <IconButton onClick={() => navigate('/admin/quiz')} sx={{ bgcolor: 'action.hover' }}>
              <BackIcon />
            </IconButton>
            <Box>
              <Typography variant="h4" sx={{ fontWeight: 'bold' }}>
                {moduleInfo.title} Quiz
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Manage quiz questions for this module
              </Typography>
            </Box>
          </Box>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenForm()}
            sx={{
              bgcolor: moduleInfo.color,
              '&:hover': { bgcolor: moduleInfo.color, filter: 'brightness(0.9)' },
            }}
          >
            Add Question
          </Button>
        </Box>
      </Paper>

      {/* Questions Table */}
      <Paper sx={{ p: 3 }}>
        {isLoading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress />
          </Box>
        ) : error ? (
          <Alert severity="error">Failed to load questions: {(error as Error).message}</Alert>
        ) : questions.length === 0 ? (
          <Box sx={{ p: 4, textAlign: 'center' }}>
            <Typography color="text.secondary" variant="h6">
              No questions yet for this module. Click "Add Question" to get started!
            </Typography>
          </Box>
        ) : (
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell><strong>#</strong></TableCell>
                  <TableCell><strong>Question</strong></TableCell>
                  <TableCell><strong>Correct Answer</strong></TableCell>
                  <TableCell align="center"><strong>Actions</strong></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {questions.map((question, index) => (
                  <TableRow key={question.id} hover>
                    <TableCell>{index + 1}</TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {question.question_text.substring(0, 100)}
                        {question.question_text.length > 100 ? '...' : ''}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={question.correct_answer}
                        sx={{
                          bgcolor: moduleInfo.color,
                          color: 'white',
                          fontWeight: 'bold',
                        }}
                        size="small"
                      />
                    </TableCell>
                    <TableCell align="center">
                      <IconButton
                        size="small"
                        onClick={() => handleDuplicate(question)}
                        color="default"
                        title="Duplicate Question"
                      >
                        <CopyIcon />
                      </IconButton>
                      <IconButton
                        size="small"
                        onClick={() => handleOpenForm(question)}
                        color="info"
                        title="Edit Question"
                      >
                        <EditIcon />
                      </IconButton>
                      <IconButton
                        size="small"
                        onClick={() => handleDelete(question.id)}
                        color="error"
                        title="Delete Question"
                      >
                        <DeleteIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Paper>

      {/* Question Form Dialog */}
      <Dialog open={formOpen} onClose={handleCloseForm} maxWidth="md" fullWidth>
        <DialogTitle>
          {editingQuestion ? '✏️ Edit Question' : '➕ Add New Question'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, mt: 2 }}>
            {/* Question Text */}
            <TextField
              label="Question"
              value={formData.question_text}
              onChange={(e) => setFormData({ ...formData, question_text: e.target.value })}
              multiline
              rows={3}
              fullWidth
              required
              placeholder="Enter your question here..."
            />

            <Divider />

            {/* Answer Choices */}
            <FormControl component="fieldset">
              <FormLabel component="legend">Answer Choices (Select the correct one)</FormLabel>
              <RadioGroup
                value={formData.correct_answer}
                onChange={(e) =>
                  setFormData({ ...formData, correct_answer: e.target.value as 'A' | 'B' | 'C' | 'D' })
                }
              >
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, mt: 2 }}>
                  <FormControlLabel
                    value="A"
                    control={<Radio />}
                    label="A:"
                    sx={{ minWidth: 60 }}
                  />
                  <TextField
                    value={formData.option_a}
                    onChange={(e) => setFormData({ ...formData, option_a: e.target.value })}
                    placeholder="Option A"
                    fullWidth
                    required
                    size="small"
                  />
                </Box>

                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <FormControlLabel
                    value="B"
                    control={<Radio />}
                    label="B:"
                    sx={{ minWidth: 60 }}
                  />
                  <TextField
                    value={formData.option_b}
                    onChange={(e) => setFormData({ ...formData, option_b: e.target.value })}
                    placeholder="Option B"
                    fullWidth
                    required
                    size="small"
                  />
                </Box>

                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <FormControlLabel
                    value="C"
                    control={<Radio />}
                    label="C:"
                    sx={{ minWidth: 60 }}
                  />
                  <TextField
                    value={formData.option_c}
                    onChange={(e) => setFormData({ ...formData, option_c: e.target.value })}
                    placeholder="Option C"
                    fullWidth
                    required
                    size="small"
                  />
                </Box>

                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <FormControlLabel
                    value="D"
                    control={<Radio />}
                    label="D:"
                    sx={{ minWidth: 60 }}
                  />
                  <TextField
                    value={formData.option_d}
                    onChange={(e) => setFormData({ ...formData, option_d: e.target.value })}
                    placeholder="Option D"
                    fullWidth
                    required
                    size="small"
                  />
                </Box>
              </RadioGroup>
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={handleCloseForm}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleSubmit}
            disabled={
              !formData.question_text ||
              !formData.option_a ||
              !formData.option_b ||
              !formData.option_c ||
              !formData.option_d
            }
            sx={{
              bgcolor: moduleInfo.color,
              '&:hover': { bgcolor: moduleInfo.color, filter: 'brightness(0.9)' },
            }}
          >
            {editingQuestion ? 'Update Question' : 'Add Question'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
