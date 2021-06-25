/*
MIT License

Copyright (c) 2021 Universidad de los Andes - ISIS2603

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package co.edu.uniandes.dse.bookstore.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.uniandes.dse.bookstore.entities.BookEntity;
import co.edu.uniandes.dse.bookstore.entities.EditorialEntity;
import co.edu.uniandes.dse.bookstore.exceptions.EntityNotFoundException;
import co.edu.uniandes.dse.bookstore.exceptions.IllegalOperationException;
import co.edu.uniandes.dse.bookstore.repositories.BookRepository;
import co.edu.uniandes.dse.bookstore.repositories.EditorialRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Clase que implementa la conexión con la persistencia para la relación entre
 * la entidad Editorial y Book.
 *
 * @author ISIS2603
 */
@Slf4j
@Data
@Service
public class EditorialBookService {

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private EditorialRepository editorialRepository;

	/**
	 * Agregar un book a la editorial
	 *
	 * @param bookId      El id libro a guardar
	 * @param editorialId El id de la editorial en la cual se va a guardar el libro.
	 * @return El libro creado.
	 * @throws EntityNotFoundException 
	 */
	
	@Transactional
	public BookEntity addBook(Long bookId, Long editorialId) throws EntityNotFoundException {
		log.info("Inicia proceso de agregarle un libro a la editorial con id = {0}", editorialId);
		
		BookEntity bookEntity = bookRepository.findById(bookId).orElse(null);
		if(bookEntity == null)
			throw new EntityNotFoundException("The book with the given id was not found");
		
		EditorialEntity editorialEntity = editorialRepository.findById(editorialId).orElse(null);
		if(editorialEntity == null)
			throw new EntityNotFoundException("The editorial with the given id was not found");
		
		bookEntity.setEditorial(editorialEntity);
		log.info("Termina proceso de agregarle un libro a la editorial con id = {0}", editorialId);
		return bookEntity;
	}

	/**
	 * Retorna todos los books asociados a una editorial
	 *
	 * @param editorialsId El ID de la editorial buscada
	 * @return La lista de libros de la editorial
	 * @throws EntityNotFoundException 
	 */
	@Transactional
	public List<BookEntity> getBooks(Long editorialId) throws EntityNotFoundException {
		log.info("Inicia proceso de consultar los libros asociados a la editorial con id = {0}", editorialId);
		EditorialEntity editorialEntity = editorialRepository.findById(editorialId).orElse(null);
		if(editorialEntity == null)
			throw new EntityNotFoundException("The editorial with the given id was not found");
		
		return editorialRepository.findById(editorialId).get().getBooks();
	}

	/**
	 * Retorna un book asociado a una editorial
	 *
	 * @param editorialsId El id de la editorial a buscar.
	 * @param booksId      El id del libro a buscar
	 * @return El libro encontrado dentro de la editorial.
	 * @throws EntityNotFoundException Si el libro no se encuentra en la editorial
	 * @throws IllegalOperationException 
	 */
	@Transactional
	public BookEntity getBook(Long editorialId, Long bookId) throws EntityNotFoundException, IllegalOperationException {
		log.info("Inicia proceso de consultar el libro con id = {0} de la editorial con id = " + editorialId, bookId);
		
		EditorialEntity editorialEntity = editorialRepository.findById(editorialId).orElse(null);
		if(editorialEntity == null)
			throw new EntityNotFoundException("The editorial with the given id was not found");
		
		BookEntity bookEntity = bookRepository.findById(bookId).orElse(null);
		if(bookEntity == null)
			throw new EntityNotFoundException("The book with the given id was not found");
		
		List<BookEntity> books = editorialEntity.getBooks();
				
		int index = books.indexOf(bookEntity);
		log.info("Termina proceso de consultar el libro con id = {0} de la editorial con id = " + editorialId, bookId);
		if (index >= 0) {
			return books.get(index);
		}
		throw new IllegalOperationException("The book is not associated to the editorial");
	}

	/**
	 * Remplazar books de una editorial
	 *
	 * @param books        Lista de libros que serán los de la editorial.
	 * @param editorialsId El id de la editorial que se quiere actualizar.
	 * @return La lista de libros actualizada.
	 * @throws EntityNotFoundException 
	 */
	@Transactional
	public List<BookEntity> replaceBooks(Long editorialId, List<BookEntity> books) throws EntityNotFoundException {
		log.info("Inicia proceso de actualizar la editorial con id = {0}", editorialId);
		EditorialEntity editorialEntity = editorialRepository.findById(editorialId).orElse(null);
		if(editorialEntity == null)
			throw new EntityNotFoundException("The editorial with the given id was not found");
		
		for (BookEntity book : books) {
			BookEntity b = bookRepository.findById(book.getId()).orElse(null);
			if(b == null)
				throw new EntityNotFoundException("The book with the given id was not found");
			
			if (books.contains(book)) {
				book.setEditorial(editorialEntity);
			} else if (book.getEditorial() != null && book.getEditorial().equals(editorialEntity)) {
				book.setEditorial(null);
			}
		}
		log.info("Termina proceso de actualizar la editorial con id = {0}", editorialId);
		return books;
	}
}