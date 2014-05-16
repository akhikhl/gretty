jQuery.noConflict();

jQuery(function($) {
  try {
  $('#sendRequest').click(function() {
    $.post('/MyWebService/getdate').done(function(data) {
      alert('done: ' + data);
    }).fail(function(jqXHR, textStatus, errorThrown) {
      $('#result').addClass('bg-danger').text('error: ' + errorThrown);
    });
  });
  } catch(e) {
    alert(e);
  }
});
